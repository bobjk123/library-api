package com.gmao.itm.library_api.service;

import com.gmao.itm.library_api.dto.CategoryRequest;
import com.gmao.itm.library_api.dto.CategoryResponse;
import com.gmao.itm.library_api.exception.BusinessRuleException;
import com.gmao.itm.library_api.exception.ResourceNotFoundException;
import com.gmao.itm.library_api.mapper.CategoryMapper;
import com.gmao.itm.library_api.model.Category;
import com.gmao.itm.library_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        Category category = getCategoryOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessRuleException("A category with name '" + request.name() + "' already exists");
        }
        Category category = categoryMapper.toEntity(request);
        category.setName(request.name().trim());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategoryOrThrow(id);
        categoryRepository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A category with name '" + request.name() + "' already exists");
                });

        categoryMapper.updateEntityFromRequest(request, category);
        category.setName(request.name().trim());
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    public Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}