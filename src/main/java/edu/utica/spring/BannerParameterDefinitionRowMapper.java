package edu.utica.spring;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BannerParameterDefinitionRowMapper implements RowMapper<BannerParameterDefinition> {

    @Override
    public BannerParameterDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        BannerParameterDefinition def = new BannerParameterDefinition();
        def.setJob(rs.getString("job"));
        def.setNumber(rs.getString("number"));
        def.setDescription(rs.getString("description"));
        def.setLength(rs.getInt("length"));
        def.setType(rs.getString("type"));
        def.setOptionalInd(rs.getString("optional_ind"));
        def.setSingleInd(rs.getString("single_ind"));
        def.setLowRange(rs.getString("low_range"));
        def.setHighRange(rs.getString("high_range"));
        def.setHelpText(rs.getString("help_text"));
        def.setValidation(rs.getString("validation"));
        def.setListValues(rs.getString("list_values"));
        def.setHasPval(rs.getString("has_pval"));

        if ("Y".equals(def.getHasPval())) {
            try (ResultSet rsPval = (ResultSet) rs.getObject("pval_list")) {
                while (rsPval.next()) {
                    def.getPvalList().add(rsPval.getString("pval"));
                }
            }
        }

        return def;
    }
}
