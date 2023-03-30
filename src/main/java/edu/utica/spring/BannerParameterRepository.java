package edu.utica.spring;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BannerParameterRepository {

    private final JdbcTemplate jdbcTemplate;

    public BannerParameterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Absolute simplest and abstracted querying with JdbcTemplate & BeanPropertyRowMapper
     * The downside with this technique is it doesn't seem capable of auto-magically handling a nested CURSOR like some of these other examples.
     * @param jobName
     * @return
     */
    public List<BannerParameterDefinition> queryBannerParameterDefinitionsBeanPropertyRowMapper(String jobName) {
        List<BannerParameterDefinition> list =
                jdbcTemplate.query("""
                        select gjbpdef_job job,
                               gjbpdef_number "number",
                               gjbpdef_desc description,
                               gjbpdef_length length,
                               gjbpdef_type_ind type,
                               gjbpdef_optional_ind optional_ind,
                               gjbpdef_single_ind single_ind,
                               gjbpdef_low_range low_range,
                               gjbpdef_high_range high_range,
                               gjbpdef_help_text help_text,
                               gjbpdef_validation validation,
                               gjbpdef_list_values list_values,
                               (select decode(count(*),0,'N','Y') from gjbpval
                                 where gjbpval_job = gjbpdef_job
                                   and gjbpval_number = gjbpdef_number) has_pval
                          from gjbpdef
                         where gjbpdef_job = ?
                        order by gjbpdef_number
                        """,
                        new BeanPropertyRowMapper<>(BannerParameterDefinition.class),
                        new Object[]{jobName});
        return list;
    }

    /**
     * This is an example of a custom RowMapper, unlike the beanPropertyRowMapper you have to create a class that extends RowMapper
     * This however allows you to include custom mapping logic like parsing an inner CURSOR object
     * @param jobName
     * @return
     */
    public List<BannerParameterDefinition> queryBannerParameterDefinitionsRowMapper(String jobName) {
        List<BannerParameterDefinition> list =
                jdbcTemplate.query("""
                        select gjbpdef_job job,
                               gjbpdef_number "number",
                               gjbpdef_desc description,
                               gjbpdef_length length,
                               gjbpdef_type_ind type,
                               gjbpdef_optional_ind optional_ind,
                               gjbpdef_single_ind single_ind,
                               gjbpdef_low_range low_range,
                               gjbpdef_high_range high_range,
                               gjbpdef_help_text help_text,
                               gjbpdef_validation validation,
                               gjbpdef_list_values list_values,
                               (select decode(count(*),0,'N','Y') from gjbpval
                                 where gjbpval_job = gjbpdef_job
                                   and gjbpval_number = gjbpdef_number) has_pval,
                               CURSOR(select gjbpval_value pval from gjbpval
                                       where gjbpval_job = gjbpdef_job
                                         and gjbpval_number = gjbpdef_number
                                      order by gjbpval_value asc) pval_list
                          from gjbpdef
                         where gjbpdef_job = ?
                        order by gjbpdef_number
                        """,
                        new BannerParameterDefinitionRowMapper(),
                        new Object[]{jobName});
        return list;
    }

    /**
     * Example using lambda RowCallbackHandler, you could also use a anonymous inner class RowCallbackHandler, or move the logic to a separate class.
     * Major difference from some of the other examples is this version of query does not return the list
     * instead RowCallbackHandler is a void method and you perform operations per-row of the resultSet
     * @param jobName
     * @return
     */
    public List<BannerParameterDefinition> queryBannerParameterDefinitionsRowCallbackHandler(String jobName) {
        List<BannerParameterDefinition> list = new ArrayList<>();
        jdbcTemplate.query("""
                        select gjbpdef_job job,
                               gjbpdef_number "number",
                               gjbpdef_desc description,
                               gjbpdef_length length,
                               gjbpdef_type_ind type,
                               gjbpdef_optional_ind optional_ind,
                               gjbpdef_single_ind single_ind,
                               gjbpdef_low_range low_range,
                               gjbpdef_high_range high_range,
                               gjbpdef_help_text help_text,
                               gjbpdef_validation validation,
                               gjbpdef_list_values list_values,
                               (select decode(count(*),0,'N','Y') from gjbpval
                                 where gjbpval_job = gjbpdef_job
                                   and gjbpval_number = gjbpdef_number) has_pval,
                               CURSOR(select gjbpval_value pval from gjbpval
                                       where gjbpval_job = gjbpdef_job
                                         and gjbpval_number = gjbpdef_number
                                      order by gjbpval_value asc) pval_list
                          from gjbpdef
                         where gjbpdef_job = ?
                        order by gjbpdef_number
                        """,
                (rs) -> {
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

                    list.add(def);
                },
                        new Object[]{jobName});

        return list;
    }

    /**
     * Example using lambda ResultSetExtractor, could also be anonymous innerclass or moved to separate class
     * processing entire ResultSet and returning List of object
     * Simpler choice is to use RowMapper, or BeanPropertyRowMapper if possible
     * @param jobName
     * @return
     */
    public List<BannerParameterDefinition> queryBannerParameterDefinitionsResultSetExtractor(String jobName) {
        List<BannerParameterDefinition> list = jdbcTemplate.query("""
                        select gjbpdef_job job,
                               gjbpdef_number "number",
                               gjbpdef_desc description,
                               gjbpdef_length length,
                               gjbpdef_type_ind type,
                               gjbpdef_optional_ind optional_ind,
                               gjbpdef_single_ind single_ind,
                               gjbpdef_low_range low_range,
                               gjbpdef_high_range high_range,
                               gjbpdef_help_text help_text,
                               gjbpdef_validation validation,
                               gjbpdef_list_values list_values,
                               (select decode(count(*),0,'N','Y') from gjbpval
                                 where gjbpval_job = gjbpdef_job
                                   and gjbpval_number = gjbpdef_number) has_pval,
                               CURSOR(select gjbpval_value pval from gjbpval
                                       where gjbpval_job = gjbpdef_job
                                         and gjbpval_number = gjbpdef_number
                                      order by gjbpval_value asc) pval_list
                          from gjbpdef
                         where gjbpdef_job = ?
                        order by gjbpdef_number
                        """,
                rs -> {
                    List<BannerParameterDefinition> list1 = new ArrayList<>();
                    while (rs.next()) {
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

                        list1.add(def);
                    }
                    return list1;
                },
                new Object[]{jobName});

        return list;
    }


}
