package com.sora.splineroads.compat.mtr;

import com.sora.splineroads.core.RailbedPath;
import com.sora.splineroads.core.RoadGeometry.V;
import com.sora.splineroads.world.RailbedTool;
import com.sora.splineroads.world.RoadBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reflective port of the upstream MTR 4 adapter. Only the MTR simulator owner
 * thread enters here; no live Rail escapes to Minecraft threads.
 */
final class Mtr4Adapter {
  private record Edge(Object rail, Object from, Object to, boolean reverse) {}
  private static final Map<Object, Task> tasks = Collections.synchronizedMap(new WeakHashMap<>());

  static boolean available() { return MtrReflection.INSTANCE != null; }
  static boolean active() { return !tasks.isEmpty(); }

  static boolean trainNode(Object block) {
    MtrReflection r = MtrReflection.INSTANCE;
    return r != null && r.blockNode.isInstance(block)
        && MtrReflection.get(r.nodeTransportMode, block) == r.train;
  }
  static boolean railbedItem(Object item) {
    MtrReflection r = MtrReflection.INSTANCE;
    return r != null && r.holderItem.isInstance(item)
        && MtrReflection.get(r.itemData, item) instanceof RailbedTool;
  }
  static Object nodeShape(Object original, Object world, Object position) {
    MtrReflection r = MtrReflection.INSTANCE;
    if (r == null || world == null || position == null) return original;
    Object view = MtrReflection.get(r.viewData, world);
    Object pos = MtrReflection.get(r.posData, position);
    if (!(view instanceof BlockGetter getter) || !(pos instanceof BlockPos at)) return original;
    var index = RoadBlocks.index(getter);
    if (index == null) return original;
    for (UUID id : index.inChunk(ChunkPos.asLong(at.getX() >> 4, at.getZ() >> 4))) {
      var built = index.roads.get(id);
      if (built == null || !built.record.railway() || built.boxes(at).isEmpty()) continue;
      Object originalData = MtrReflection.get(r.shapeData, original);
      if (!(originalData instanceof VoxelShape base)) return original;
      return r.newVoxelShape(Shapes.or(base, index.shape(at)));
    }
    return original;
  }

  static void tick(Object value) {
    MtrReflection r = MtrReflection.INSTANCE;
    if (r == null || !r.simulator.isInstance(value)) return;
    Task task = tasks.get(value);
    if (task == null) {
      for (MtrRailAccess.Request request : MtrRailAccess.requests) {
        if (request.dimension().equals(r.dimension(value)) && MtrRailAccess.requests.remove(request)) {
          if (request.result().isDone()) continue;
          try { task = new Task(value, request); tasks.put(value, task); }
          catch (RuntimeException failure) { request.result().completeExceptionally(failure); }
          break;
        }
      }
    }
    if (task == null) return;
    try {
      long deadline = System.nanoTime() + 2_000_000;
      int steps = 0;
      while (!task.request.result().isDone() && steps++ < 256 && System.nanoTime() < deadline) task.step(value);
    } catch (RuntimeException failure) { task.request.result().completeExceptionally(failure); }
    if (task.request.result().isDone()) tasks.remove(value);
  }

  private static Object position(RailbedPath.Node p) {
    return MtrReflection.INSTANCE.newPosition(p.x(), p.y(), p.z());
  }
  private static RailbedPath.Node node(Object p) {
    MtrReflection r = MtrReflection.INSTANCE;
    return new RailbedPath.Node(r.intX(p), r.intY(p), r.intZ(p));
  }
  private static List<Edge> path(Object simulator, Object a, Object b) {
    MtrReflection r = MtrReflection.INSTANCE;
    Object direct = r.neighbours(simulator, a).get(b);
    if (r.trainRail(direct)) return List.of(edge(direct, a, b));
    var starts = r.neighbours(simulator, a).entrySet().stream()
        .filter(e -> r.trainRail(e.getValue())).toList();
    if (starts.size() > 2) throw new IllegalArgumentException("起点有道岔，请分段选择");
    var found = new ArrayList<List<Edge>>();
    boolean ambiguous = false;
    for (var start : starts) {
      var edges = new ArrayList<Edge>();
      var visited = new HashSet<Object>();
      visited.add(a);
      Object previous = a, current = start.getKey(), rail = start.getValue();
      double length = 0;
      while (true) {
        if (!visited.add(current)) { ambiguous = true; break; }
        length += r.length(rail);
        if (edges.size() >= RailbedPath.MAX_SEGMENTS || length > RailbedPath.MAX_LENGTH) { ambiguous = true; break; }
        edges.add(edge(rail, previous, current));
        if (current.equals(b)) { found.add(List.copyOf(edges)); break; }
        Object before = previous;
        var next = r.neighbours(simulator, current).entrySet().stream()
            .filter(e -> !e.getKey().equals(before) && r.trainRail(e.getValue())).toList();
        if (next.size() != 1) { ambiguous |= next.size() > 1; break; }
        previous = current; current = next.get(0).getKey(); rail = next.get(0).getValue();
      }
    }
    if (found.size() != 1 || ambiguous)
      throw new IllegalArgumentException(found.isEmpty() && !ambiguous
          ? "请先使用 MTR 连接这两个轨道节点" : "铁路途中存在道岔、环线或超长区间，请分段选择");
    return found.get(0);
  }
  private static Edge edge(Object rail, Object a, Object b) {
    return new Edge(rail, a, b, MtrReflection.compare(a, b));
  }

  private static final class Task {
    final MtrRailAccess.Request request;
    final List<Edge> edges;
    final List<RailbedPath.Segment> output = new ArrayList<>();
    RailbedPath.Sampler sampler;
    int index, total;
    Task(Object simulator, MtrRailAccess.Request request) {
      this.request = request;
      edges = path(simulator, position(request.a()), position(request.b()));
    }
    void step(Object simulator) {
      MtrReflection r = MtrReflection.INSTANCE;
      if (System.nanoTime() > request.deadline()) throw new IllegalArgumentException("铁路读取超时，请缩短区间");
      for (Edge edge : edges)
        if (r.railById(simulator).get(r.hexId(edge.rail())) != edge.rail())
          throw new IllegalArgumentException("轨道已变化，请重新选择");
      Edge edge = edges.get(index);
      if (sampler == null) {
        sampler = new RailbedPath.Sampler(r.length(edge.rail()), s -> {
          Object point = r.point(edge.rail(), s, edge.reverse());
          return new V(MtrReflection.number(MtrReflection.get(r.curveX, point)),
              MtrReflection.number(MtrReflection.get(r.curveY, point)),
              MtrReflection.number(MtrReflection.get(r.curveZ, point)));
        });
      }
      sampler.step();
      if (!sampler.done()) return;
      var points = sampler.result();
      total += points.size();
      if (total > RailbedPath.MAX_POINTS) throw new IllegalArgumentException("铁路采样超过预算，请分段选择");
      output.add(new RailbedPath.Segment(r.hexId(edge.rail()), node(edge.from()), node(edge.to()),
          edge.reverse(), r.length(edge.rail()), r.styles(edge.rail()), points));
      sampler = null;
      if (++index == edges.size()) request.result().complete(new RailbedPath.Path(output));
    }
  }
  private Mtr4Adapter() {}
}
