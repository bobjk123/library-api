package com.gmao.itm.library_api.service;

import com.gmao.itm.library_api.dto.BookRequest;
import com.gmao.itm.library_api.dto.BookResponse;
import com.gmao.itm.library_api.exception.BusinessRuleException;
import com.gmao.itm.library_api.exception.ResourceNotFoundException;
import com.gmao.itm.library_api.mapper.BookMapper;
import com.gmao.itm.library_api.model.Book;
import com.gmao.itm.library_api.model.Category;
import com.gmao.itm.library_api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final BookMapper bookMapper;

    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    public BookResponse findById(Long id) {
        return bookMapper.toResponse(getBookOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        validateCopies(request.totalCopies(), request.availableCopies());
        if (bookRepository.existsByIsbn(request.isbn().trim())) {
            throw new BusinessRuleException("A book with ISBN '" + request.isbn() + "' already exists");
        }

        Category category = categoryService.getCategoryOrThrow(request.categoryId());
        Book book = bookMapper.toEntity(request);
        book.setIsbn(request.isbn().trim());
        book.setCategory(category);

        Book saved = bookRepository.save(book);
        return bookMapper.toResponse(saved);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        validateCopies(request.totalCopies(), request.availableCopies());
        Book book = getBookOrThrow(id);

        bookRepository.findByIsbn(request.isbn().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A book with ISBN '" + request.isbn() + "' already exists");
                });

        Category category = categoryService.getCategoryOrThrow(request.categoryId());
        bookMapper.updateEntityFromRequest(request, book);
        book.setIsbn(request.isbn().trim());
        book.setCategory(category);

        Book updated = bookRepository.save(book);
        return bookMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }

    public Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private void validateCopies(Integer totalCopies, Integer availableCopies) {
        if (availableCopies > totalCopies) {
            throw new BusinessRuleException("Available copies cannot exceed total copies");
        }
    }
}
