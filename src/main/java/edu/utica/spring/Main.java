package edu.utica.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    JdbcTemplate jdbcTemplate;
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        BannerParameterRepository bannerParameterRepository =
                new BannerParameterRepository(jdbcTemplate);

        // Example using BeanPropertyRowMapper to process ResultSet
//        for (BannerParameterDefinition d : bannerParameterRepository.queryBannerParameterDefinitionsBeanPropertyRowMapper("SYRRSTU")) {
//            System.out.println(d);
//        }

        // Example using custom RowMapper e.g. BannerParameterDefinitionRowMapper
//        for (BannerParameterDefinition d : bannerParameterRepository.queryBannerParameterDefinitionsRowMapper("SYRRSTU")) {
//            System.out.println(d);
//        }

        // Example using RowCallbackHandler
//        for (BannerParameterDefinition d : bannerParameterRepository.queryBannerParameterDefinitionsRowCallbackHandler("SYRRSTU")) {
//            System.out.println(d);
//        }

        // Example using ResultSetExtractor
        for (BannerParameterDefinition d : bannerParameterRepository.queryBannerParameterDefinitionsResultSetExtractor("SYRRSTU")) {
            System.out.println(d);
        }

    }
}
