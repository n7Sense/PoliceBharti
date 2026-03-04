package com.nst.ufrs.init;

import com.nst.ufrs.repository.GlobalRunningNumberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalRunningNumberConfig {

    @Autowired
    GlobalRunningNumberRepository globalRunningNumberRepository;

    @PostConstruct
    public void initGlobalRunningNumber(){

    }
}
