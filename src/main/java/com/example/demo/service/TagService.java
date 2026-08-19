package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Tag;
import com.example.demo.repository.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findOrCreate(String name) {

        String trimmedName = name.trim();

        return tagRepository
                .findByName(trimmedName)
                .orElseGet(() -> {

                    Tag tag = new Tag();
                    tag.setName(trimmedName);

                    return tagRepository.save(tag);
                });
    }
}