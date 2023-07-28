package com.example.stock.facade;

import com.example.stock.domain.Stock;
import com.example.stock.reposiotry.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OptimisticLockStockFacadeServiceTest {

    @Autowired
    private OptimisticLockStockFacadeService optimisticLockStockFacadeService;

    @Autowired
    private StockRepository repository;

    @BeforeEach
    private void before() {
        Stock stock = new Stock(1L, 100L);
        repository.save(stock);
    }

    @AfterEach
    private void after() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("재고를 1 감소시킨다.")
    void stockDecrease() throws InterruptedException {
        optimisticLockStockFacadeService.decrease(1L, 1L);

        Stock stock = repository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(99L);
    }

    @Test
    void 동시에_100개_감소_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); //멀티 쓰레드를 사용하기 위함. 비동기로 실행하는 작업을 단순화하게 사용할 수 있도록 하는 자바 API

        CountDownLatch latch = new CountDownLatch(threadCount); //100개의 요청을 카운트하기 위함.

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() ->
            {
                try {
                    optimisticLockStockFacadeService.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown(); // 카운트
                }
            });
        }

        latch.await();//다른 쓰레드에서 실행 중인 작업이 완료될 때 까지 대기
        Stock stock = repository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}