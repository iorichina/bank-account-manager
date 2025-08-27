package iorihuang.bankaccountmanager.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * JMH基准测试启动类
 * 用于运行所有基准测试，并确保Spring上下文只启动一次
 */
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JmhBenchmarkRunner {
    
    private JmhSpringContext springContext = new JmhSpringContext();
    
    @Setup(Level.Trial)
    public void setup() {
        System.out.println("JmhBenchmarkRunner setup - Initializing shared Spring context");
        springContext.setup();
    }
    
    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println("JmhBenchmarkRunner tearDown - Closing shared Spring context");
        JmhSpringContext.closeContext();
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BankAccountServiceCreateAccountBenchmark.class.getSimpleName())
                .include(BankAccountServiceGetAccountBenchmark.class.getSimpleName())
                .include(BankAccountServiceUpdateAccountBenchmark.class.getSimpleName())
                .include(BankAccountServiceTransferBenchmark.class.getSimpleName())
                .include(BankAccountServiceListAccountsBenchmark.class.getSimpleName())
                .include(BankAccountServiceDeleteAccountBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}