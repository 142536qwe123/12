// 抽象基类
public abstract class Task {
    public abstract int getDuration();
    public abstract String toString();
}

// 原子任务
class AtomicTask extends Task {
    private final int time;

    public AtomicTask(int time) {
        this.time = time;
    }

    @Override
    public int getDuration() {
        return time;
    }

    @Override
    public String toString() {
        return String.valueOf(time);
    }
}

// 组合任务抽象类
abstract class CompositeTask extends Task {
    protected Task left;
    protected Task right;

    public CompositeTask(Task left, Task right) {
        this.left = left;
        this.right = right;
    }
}

// 串行任务
class SerialTask extends CompositeTask {
    public SerialTask(Task left, Task right) {
        super(left, right);
    }

    @Override
    public int getDuration() {
        return left.getDuration() + right.getDuration();
    }

    @Override
    public String toString() {
        return "(S, " + left.toString() + ", " + right.toString() + ")";
    }
}

// 并行任务
class ParallelTask extends CompositeTask {
    public ParallelTask(Task left, Task right) {
        super(left, right);
    }

    @Override
    public int getDuration() {
        return Math.max(left.getDuration(), right.getDuration());
    }

    @Override
    public String toString() {
        return "(P, " + left.toString() + ", " + right.toString() + ")";
    }
}

