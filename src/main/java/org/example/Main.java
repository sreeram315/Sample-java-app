package org.example;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;
import org.example.dao.TestModelMapper;
import org.example.db.ConnectionPoolFactory;
import org.example.model.TestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting ... ");
        int nThreads = 100, nExecs = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<? extends Future<?>> futures = IntStream.range(0, nExecs)
                .mapToObj(i -> (Runnable) Main::runGetTradeQuery)
                .map(executorService::submit)
                .toList();
        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
        logger.info("Complete ...");
    }

    private static void runGetTradeQuery() {
        try (SqlSession session = ConnectionPoolFactory.createSessionFactory().openSession()) {
            TestModelMapper mapper = session.getMapper(TestModelMapper.class);
            long startTime = System.nanoTime();
            TestModel trade = mapper.getOne();
            logger.info("Result length: {} | TT: {}", trade != null ? 1 : 0, (System.nanoTime() - startTime) / 1_000_000_000.0);
        } catch (Exception e) {
            logger.error(ExceptionUtil.unwrapThrowable(e).getMessage().replace("\n", " "));
        }
    }
}