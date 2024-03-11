package com.mnsoo.dividends.service;

import com.mnsoo.dividends.exception.impl.NoCompanyException;
import com.mnsoo.dividends.model.Company;
import com.mnsoo.dividends.model.Dividend;
import com.mnsoo.dividends.model.ScrapedResult;
import com.mnsoo.dividends.model.constants.CacheKey;
import com.mnsoo.dividends.persist.CompanyRespository;
import com.mnsoo.dividends.persist.DividendRepository;
import com.mnsoo.dividends.persist.entity.CompanyEntity;
import com.mnsoo.dividends.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {
    private final CompanyRespository companyRespository;
    private final DividendRepository dividendRepository;

    // 요청이 자주 들어오는가?
    // 자주 변경되는 데이터 인가?
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName){
        log.info("search company -> " + companyName);
        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRespository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(
                new Company(company.getTicker(), company.getName()), dividends
        );
    }
}
