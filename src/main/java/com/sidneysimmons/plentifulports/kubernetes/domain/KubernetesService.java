package com.sidneysimmons.plentifulports.kubernetes.domain;

import lombok.Data;

/**
 * Kubernetes service.
 * 
 * @author Sidney Simmons
 */
@Data
public class KubernetesService {

    private String name;
    private String namespace;

}
