package com.mnsoo.dividends.scheduler;

import com.mnsoo.dividends.model.Company;
import com.mnsoo.dividends.model.ScrapedResult;
import com.mnsoo.dividends.model.constants.CacheKey;
import com.mnsoo.dividends.persist.CompanyRespository;
import com.mnsoo.dividends.persist.DividendRepository;
import com.mnsoo.dividends.persist.entity.CompanyEntity;
import com.mnsoo.dividends.persist.entity.DividendEntity;
import com.mnsoo.dividends.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {

    private final CompanyRespository companyRespository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinaceScraper;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRespository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            ScrapedResult scrapedResult = this.yahooFinaceScraper.scrap(
                    new Company(company.getTicker(), company.getName())
            );

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // dividend 모델을 dividend entity로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // element를 하나씩 dividend repository에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository
                                .existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3초
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
