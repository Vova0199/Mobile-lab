package com.nulp.labs_aplication.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Vova0199 on 18/11/2018.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "id"
})
public class ProductionCompany {

    @JsonProperty("name")
    public String name;
    @JsonProperty("id")
    public int id;

}
