package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.model.Tag;
import com.example.demo.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;

    @BeforeEach
    void setUp() {

        tagService =
                new TagService(tagRepository);
    }


    // =========================
    // findAll
    // =========================

    @Test
    void findAll_タグ一覧を取得できる() {

        Tag tag1 = createTag(
                1L,
                "重要");

        Tag tag2 = createTag(
                2L,
                "横浜");

        List<Tag> tags =
                List.of(
                        tag1,
                        tag2);

        when(tagRepository.findAll())
                .thenReturn(tags);

        List<Tag> result =
                tagService.findAll();

        assertEquals(
                2,
                result.size());

        assertEquals(
                "重要",
                result.get(0).getName());

        assertEquals(
                "横浜",
                result.get(1).getName());

        verify(tagRepository)
                .findAll();
    }


    // =========================
    // findOrCreate
    // =========================

    @Test
    void findOrCreate_既存タグが存在すればそのタグを返す() {

        Tag existingTag =
                createTag(
                        1L,
                        "重要");

        when(tagRepository.findByName("重要"))
                .thenReturn(
                        Optional.of(existingTag));

        Tag result =
                tagService.findOrCreate("重要");

        assertSame(
                existingTag,
                result);

        verify(tagRepository)
                .findByName("重要");
    }

    @Test
    void findOrCreate_存在しないタグなら新規作成する() {

        when(tagRepository.findByName("重要"))
                .thenReturn(
                        Optional.empty());

        Tag savedTag =
                createTag(
                        1L,
                        "重要");

        when(tagRepository.save(
                any(Tag.class)))
                .thenReturn(savedTag);

        Tag result =
                tagService.findOrCreate("重要");

        assertSame(
                savedTag,
                result);

        ArgumentCaptor<Tag> captor =
                ArgumentCaptor.forClass(
                        Tag.class);

        verify(tagRepository)
                .save(captor.capture());

        Tag createdTag =
                captor.getValue();

        assertEquals(
                "重要",
                createdTag.getName());
    }

    @Test
    void findOrCreate_タグ名の前後空白を除去できる() {

        Tag existingTag =
                createTag(
                        1L,
                        "重要");

        when(tagRepository.findByName("重要"))
                .thenReturn(
                        Optional.of(existingTag));

        Tag result =
                tagService.findOrCreate(
                        "  重要  ");

        assertSame(
                existingTag,
                result);

        verify(tagRepository)
                .findByName("重要");
    }


    // =========================
    // テストデータ作成
    // =========================

    private Tag createTag(
            Long id,
            String name) {

        Tag tag =
                new Tag();

        tag.setId(id);
        tag.setName(name);

        return tag;
    }
}