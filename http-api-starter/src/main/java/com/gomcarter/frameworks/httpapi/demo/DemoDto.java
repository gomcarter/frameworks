package com.gomcarter.frameworks.httpapi.demo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author gomcarter
 */
@Data
@Accessors(chain = true)
public class DemoDto {

    private Long id;

    private String nickname;

    private String openId;
}
