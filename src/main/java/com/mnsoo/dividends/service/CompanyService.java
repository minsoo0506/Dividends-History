package com.mnsoo.dividends.service;

import com.mnsoo.dividends.exception.impl.NoCompanyException;
import com.mnsoo.dividends.model.Company;
import com.mnsoo.dividends.model.ScrapedResult;
import com.mnsoo.dividends.persist.CompanyRespository;
import com.mnsoo.dividends.persist.DividendRepository;
import com.mnsoo.dividends.persist.entity.CompanyEntity;
import com.mnsoo.dividends.persist.entity.DividendEntity;
import com.mnsoo.dividends.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {
    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRespository companyRespository;
    private final DividendRepository dividendRepository;
    public Company save(String ticker) {
        boolean exists = this.companyRespository.existsByTicker(ticker);
        if(exists){
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRespository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker){
        // ticker를 기준으로 회사 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRespository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                                                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                    .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword){
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRespository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword){
        this.trie.put(keyword, null);
    }

    public List<String> autoComplete(String keyword){
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .collect(Collectors.toList());
    }

    public void deleteAutoCompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker){
        var company = this.companyRespository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRespository.delete(company);

        this.deleteAutoCompleteKeyword(company.getName());
        return company.getName();
    }
}
