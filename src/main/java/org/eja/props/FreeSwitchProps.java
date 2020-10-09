package org.eja.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "freeswitch")
public class FreeSwitchProps {
    private String userDirectoryPath;
    private String webapi;
}
