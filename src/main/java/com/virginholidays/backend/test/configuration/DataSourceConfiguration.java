package com.virginholidays.backend.test.configuration;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * The DataSourceConfiguration
 *
 * @author Geoff Perks
 */
@Configuration
@ConfigurationProperties("backend-test.data-source")
@Validated
public class DataSourceConfiguration {

    @NotEmpty
    private String csvLocation;

    /**
     * Gets the CSV location
     *
     * @return the CSV location
     */
    public String getCsvLocation() {
        return csvLocation;
    }

    /**
     * Sets the CSV location
     *
     * @param csvLocation the CSV location
     */
    public void setCsvLocation(String csvLocation) {
        this.csvLocation = csvLocation;
    }
}
