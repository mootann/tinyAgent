package com.tinyflow.memory;

import com.tinyflow.model.Fact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of MemoryStorage.
 */
@Component
public class InMemoryStorage implements MemoryStorage {

    private final List<Fact> facts = new CopyOnWriteArrayList<>();

    @Override
    public List<Fact> getFacts() {
        return new ArrayList<>(facts);
    }

    @Override
    public void saveFacts(List<Fact> newFacts) {
        facts.clear();
        facts.addAll(newFacts);
    }

}
