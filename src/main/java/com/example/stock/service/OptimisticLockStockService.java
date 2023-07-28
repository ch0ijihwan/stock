package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.reposiotry.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }


    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdOptimisticLock(id);
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
