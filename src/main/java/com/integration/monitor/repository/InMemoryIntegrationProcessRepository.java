package com.integration.monitor.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.integration.monitor.model.IntegrationProcess;
//import org.springframework.stereotype.Repository;

//@Repository
public class InMemoryIntegrationProcessRepository
        //implements IntegrationProcessRepository 
        {

    private final List<IntegrationProcess> processes = new ArrayList<>();

    //@Override
    public IntegrationProcess save(IntegrationProcess process) {

        processes.removeIf(
            existing ->
                    existing.getId().equals(process.getId())
        );
        
        processes.add(process);

        return process;
    }

   //@Override
    public List<IntegrationProcess> findAll() {

        return new ArrayList<>(processes);
    }

    //@Override
    public Optional<IntegrationProcess> findById(Long id) {

        return processes.stream()
                .filter(process -> process.getId().equals(id))
                .findFirst();
    }

    //@Override
    public boolean deleteById(Long id) {

        return processes.removeIf(
                process -> process.getId().equals(id)
        );
    }
}