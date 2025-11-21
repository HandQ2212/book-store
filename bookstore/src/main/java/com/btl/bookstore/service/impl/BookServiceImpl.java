package com.btl.bookstore.service.impl;

import com.btl.bookstore.model.Book;
import com.btl.bookstore.repository.BookRepository;
import com.btl.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Page<Book> getAllBooksPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return bookRepository.findAll(pageable);
    }

    @Override
    public Boolean deleteBook(Integer id) {
        Book book = bookRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(book)) {
            bookRepository.delete(book);
            return true;
        }
        return  false;
    }

    @Override
    public Book getBookById(Integer id) {
        Book book = bookRepository.findById(id).orElse(null);
        return book;
    }

    @Override
    public Book updateBook(Book book, MultipartFile image) {
        Book dbBook = getBookById(book.getId());

        String imageName = image.isEmpty() ? dbBook.getImage() : image.getOriginalFilename();

        dbBook.setTitle(book.getTitle());
        dbBook.setDescription(book.getDescription());
        dbBook.setCategory(book.getCategory());
        dbBook.setPrice(book.getPrice());
        dbBook.setStock(book.getStock());
        dbBook.setImage(imageName);
        dbBook.setIsActive(book.getIsActive());
        dbBook.setDiscount(book.getDiscount());

        // 5=100*(5/100); 100-5=95
        Double disocunt = book.getPrice() * (book.getDiscount() / 100.0);
        Double discountPrice = book.getPrice() - disocunt;
        dbBook.setDiscountPrice(discountPrice);

        Book updateBook = bookRepository.save(dbBook);

        if (!ObjectUtils.isEmpty(updateBook)) {

            if (!image.isEmpty()) {

                try {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "book_img" + File.separator
                            + image.getOriginalFilename());
                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return book;
        }
        return null;
    }

    @Override
    public List<Book> getAllActiveBooks(String category) {
        List<Book> books = null;
        if (ObjectUtils.isEmpty(category)) {
            books = bookRepository.findByIsActiveTrue();
        } else {
            books = bookRepository.findByCategory(category);
        }

        return books;
    }

    @Override
    public List<Book> searchBook(String ch) {
        return bookRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }

    @Override
    public Page<Book> searchBookPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return bookRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
    }

    @Override
    public Page<Book> getAllActiveBookPagination(Integer pageNo, Integer pageSize, String category) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Book> pageBook = null;

        if (ObjectUtils.isEmpty(category)) {
            pageBook = bookRepository.findByIsActiveTrue(pageable);
        } else {
            pageBook = bookRepository.findByCategory(pageable, category);
        }
        return pageBook;
    }

    @Override
    public Page<Book> searchActiveBookPagination(Integer pageNo, Integer pageSize, String category, String ch) {
        Page<Book> pageBook = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        pageBook = bookRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);

//        if (ObjectUtils.isEmpty(category)) {
//            pageBook = bookRepository.findByIsActiveTrue(pageable);
//        } else {
//            pageBook = bookRepository.findByCategory(pageable, category);
//        }
        return pageBook;
    }
}
