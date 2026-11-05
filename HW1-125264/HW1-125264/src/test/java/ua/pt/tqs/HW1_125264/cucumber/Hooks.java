package ua.pt.tqs.HW1_125264.cucumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import io.cucumber.java.Before;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.InMemoryLockerHardwareFake;

public class Hooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InMemoryLockerHardwareFake hardwareFake;

    @Autowired
    private StubGeocodingPort geocodingPort;

    @Before
    public void resetState() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE parcel_history, parcel, compartment, locker_station RESTART IDENTITY CASCADE");
        new ResourceDatabasePopulator(new ClassPathResource("data.sql"))
            .execute(jdbcTemplate.getDataSource());
        hardwareFake.reset();
        geocodingPort.reset();
    }
}
