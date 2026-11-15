package com.sora.splineroads.compat.mtr;

import com.sora.splineroads.core.RailbedPath;
import com.sora.splineroads.core.RoadGeometry.V;
import java.util.*;

/** Owner-thread spatial summaries, invalidated at MTR's own graph sync boundary. */
final class MtrNearby {
  private static final Map<Object, Index> indices = Collections.synchronizedMap(new WeakHashMap<>());
  static final class Index {
    final Map<Long, List<Object>> cells = new HashMap<>();
    Iterator<Object> build;
    Task task;
    boolean ready;
    int count, links;
  }
  static void changed(Object value) {
    MtrReflection r = MtrReflection.INSTANCE;
    if (r == null || !r.simulator.isInstance(value)) return;
    MtrRailAccess.changedDimension(r.dimension(value));
    Index old = indices.remove(value);
    if (old != null && old.task != null)
      old.task.request.result().completeExceptionally(new IllegalArgumentException("MTR 轨道数据已变化，请重新预览"));
  }
  static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

  static void tick(Object value) {
    MtrReflection r = MtrReflection.INSTANCE;
    if (r == null || !r.simulator.isInstance(value)) return;
    String dimension = r.dimension(value);
    boolean requested = MtrRailAccess.nearby.stream().anyMatch(q -> q.dimension().equals(dimension));
    Index index = indices.get(value);
    if (index == null && !requested) return;
    if (index == null) {
      index = new Index();
      index.build = r.rails(value).iterator();
      indices.put(value, index);
    }
    try {
      long deadline = System.nanoTime() + 2_000_000;
      int steps = 0;
      while (!index.ready && steps++ < 256 && System.nanoTime() < deadline) {
        if (!index.build.hasNext()) { index.ready = true; index.build = null; break; }
        Object rail = index.build.next();
        if (!r.trainRail(rail)) continue;
        if (++index.count > 100_000)
          throw new IllegalArgumentException("MTR 邻轨索引超过首版预算，请减少当前维度轨段数量");
        double minX = MtrReflection.number(MtrReflection.get(r.mathMinX, r.math(rail)));
        double maxX = MtrReflection.number(MtrReflection.get(r.mathMaxX, r.math(rail)));
        double minY = MtrReflection.number(MtrReflection.get(r.mathMinY, r.math(rail)));
        double maxY = MtrReflection.number(MtrReflection.get(r.mathMaxY, r.math(rail)));
        double minZ = MtrReflection.number(MtrReflection.get(r.mathMinZ, r.math(rail)));
        double maxZ = MtrReflection.number(MtrReflection.get(r.mathMaxZ, r.math(rail)));
        for (int x = Math.floorDiv((int) minX, 64); x <= Math.floorDiv((int) maxX, 64); x++)
          for (int z = Math.floorDiv((int) minZ, 64); z <= Math.floorDiv((int) maxZ, 64); z++) {
            if (++index.links > 500_000) throw new IllegalArgumentException("MTR 邻轨索引超过首版空间预算");
            index.cells.computeIfAbsent(key(x, z), k -> new ArrayList<>()).add(rail);
          }
      }
      if (!index.ready) return;
      if (index.task == null)
        for (MtrRailAccess.NearRequest request : MtrRailAccess.nearby)
          if (request.dimension().equals(dimension) && MtrRailAccess.nearby.remove(request)) {
            if (!request.result().isDone()) index.task = new Task(index, request);
            break;
          }
      if (index.task != null) {
        while (!index.task.request.result().isDone() && steps++ < 256 && System.nanoTime() < deadline)
          index.task.step(value);
        if (index.task.request.result().isDone()) index.task = null;
      }
    } catch (RuntimeException failure) {
      if (index.task != null) index.task.request.result().completeExceptionally(failure);
      for (MtrRailAccess.NearRequest request : MtrRailAccess.nearby)
        if (request.dimension().equals(dimension)) request.result().completeExceptionally(failure);
      indices.remove(value);
    }
  }

  static final class Task {
    final MtrRailAccess.NearRequest request;
    final List<Object> rails;
    final List<RailbedPath.Path> output = new ArrayList<>();
    int cursor;
    RailbedPath.Sampler sampler;
    Task(Index index, MtrRailAccess.NearRequest request) {
      this.request = request;
      MtrReflection r = MtrReflection.INSTANCE;
      var found = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
      V min = request.min(), max = request.max();
      for (int x = (int) Math.floor(min.x() / 64); x <= Math.floor(max.x() / 64); x++)
        for (int z = (int) Math.floor(min.z() / 64); z <= Math.floor(max.z() / 64); z++)
          for (Object rail : index.cells.getOrDefault(key(x, z), List.of())) {
            Object m = r.math(rail);
            double minX = MtrReflection.number(MtrReflection.get(r.mathMinX, m));
            double maxX = MtrReflection.number(MtrReflection.get(r.mathMaxX, m));
            double minY = MtrReflection.number(MtrReflection.get(r.mathMinY, m));
            double maxY = MtrReflection.number(MtrReflection.get(r.mathMaxY, m));
            double minZ = MtrReflection.number(MtrReflection.get(r.mathMinZ, m));
            double maxZ = MtrReflection.number(MtrReflection.get(r.mathMaxZ, m));
            if (maxX >= min.x() && minX <= max.x() && maxZ >= min.z() && minZ <= max.z()
                && maxY >= min.y() && minY <= max.y()) found.add(rail);
            if (found.size() > 128) throw new IllegalArgumentException("附近轨道过多，请缩短 A–B 区间");
          }
      rails = List.copyOf(found);
    }
    void step(Object simulator) {
      MtrReflection r = MtrReflection.INSTANCE;
      if (cursor == rails.size()) { request.result().complete(List.copyOf(output)); return; }
      Object rail = rails.get(cursor);
      if (r.railById(simulator).get(r.hexId(rail)) != rail)
        throw new IllegalArgumentException("附近轨道已变化，请重新预览");
      if (sampler == null)
        sampler = new RailbedPath.Sampler(r.length(rail), s -> {
          Object point = r.point(rail, s, false);
          return new V(MtrReflection.number(MtrReflection.get(r.curveX, point)),
              MtrReflection.number(MtrReflection.get(r.curveY, point)),
              MtrReflection.number(MtrReflection.get(r.curveZ, point)));
        });
      sampler.step();
      if (!sampler.done()) return;
      var points = sampler.result();
      var a = points.get(0).position();
      var b = points.get(points.size() - 1).position();
      output.add(new RailbedPath.Path(List.of(new RailbedPath.Segment(r.hexId(rail),
          node(a), node(b), false, r.length(rail), r.styles(rail), points))));
      sampler = null;
      cursor++;
    }
    static RailbedPath.Node node(V p) {
      return new RailbedPath.Node((int) Math.round(p.x() - .5), (int) Math.round(p.y()),
          (int) Math.round(p.z() - .5));
    }
  }
  private MtrNearby() {}
}
