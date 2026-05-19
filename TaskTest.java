// 测试类
public class TaskTest {
    public static void main(String[] args) {
        // 1. (S, (S, 2, 3), 4)
        Task task1 = new SerialTask(
                new SerialTask(new AtomicTask(2), new AtomicTask(3)),
                new AtomicTask(4)
        );
        System.out.println(task1 + " 总耗时 = " + task1.getDuration());

        // 2. (P, 100, (S, 50, 50))
        Task task2 = new ParallelTask(
                new AtomicTask(100),
                new SerialTask(new AtomicTask(50), new AtomicTask(50))
        );
        System.out.println(task2 + " 总耗时 = " + task2.getDuration());

        // 3. (S, (P, 10, 20), (P, 15, 5))
        Task task3 = new SerialTask(
                new ParallelTask(new AtomicTask(10), new AtomicTask(20)),
                new ParallelTask(new AtomicTask(15), new AtomicTask(5))
        );
        System.out.println(task3 + " 总耗时 = " + task3.getDuration());

        // 4. (P, (S, (P, 2, 2), 3), (S, 4, 4))
        Task task4 = new ParallelTask(
                new SerialTask(
                        new ParallelTask(new AtomicTask(2), new AtomicTask(2)),
                        new AtomicTask(3)
                ),
                new SerialTask(new AtomicTask(4), new AtomicTask(4))
        );
        System.out.println(task4 + " 总耗时 = " + task4.getDuration());
    }
}