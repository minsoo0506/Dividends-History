package com.mnsoo.dividends.scraper;

import com.mnsoo.dividends.model.Company;
import com.mnsoo.dividends.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
