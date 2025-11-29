package com.btl.bookstore.service.impl;

import com.btl.bookstore.model.Book;
import com.btl.bookstore.repository.BookRepository;
import com.btl.bookstore.repository.BookOrderRepository;
import com.btl.bookstore.service.BookService;
import com.btl.bookstore.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookOrderRepository bookOrderRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

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
            // Kiểm tra xem sách có trong đơn hàng chưa giao không
            if (bookOrderRepository.existsByBookIdAndNotDelivered(id)) {
                return false; // Không thể xóa vì sách đang có trong đơn hàng chưa giao
            }
            bookRepository.delete(book);
            return true;
        }
        return false;
    }

    @Override
    public Book getBookById(Integer id) {
        Book book = bookRepository.findById(id).orElse(null);
        return book;
    }

    @Override
    public Book updateBook(Book book, MultipartFile image) {
        Book dbBook = getBookById(book.getId());

        String imageName = dbBook.getImage();

        dbBook.setTitle(book.getTitle());
        dbBook.setDescription(book.getDescription());
        dbBook.setCategory(book.getCategory());
        dbBook.setPrice(book.getPrice());
        dbBook.setStock(book.getStock());
        dbBook.setIsActive(book.getIsActive());
        dbBook.setDiscount(book.getDiscount());

        // Tính giá sau giảm: VD: 5% = 100 * (5/100); 100 - 5 = 95
        Double disocunt = book.getPrice() * (book.getDiscount() / 100.0);
        Double discountPrice = book.getPrice() - disocunt;
        dbBook.setDiscountPrice(discountPrice);

        if (!image.isEmpty()) {
            // Xóa ảnh cũ từ Cloudinary
            if (imageName != null && !imageName.isEmpty() && imageName.startsWith("https")) {
                try {
                    String publicId = extractPublicIdFromUrl(imageName, "books");
                    if (publicId != null) {
                        cloudinaryService.deleteImage(publicId);
                    }
                } catch (Exception e) {
                    logger.warn("Không thể xóa ảnh sách cũ", e);
                }
            }
            // Tải ảnh mới lên
            try {
                imageName = cloudinaryService.uploadImage(image, "books");
            } catch (Exception e) {
                logger.error("Lỗi khi tải ảnh sách lên", e);
            }
        }

        dbBook.setImage(imageName);
        Book updateBook = bookRepository.save(dbBook);

        return !ObjectUtils.isEmpty(updateBook) ? updateBook : null;
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

    private String extractPublicIdFromUrl(String url, String folder) {
        try {
            if (url == null || !url.contains("cloudinary")) {
                return null;
            }
            // Định dạng URL: https://res.cloudinary.com/da4dr8ghb/image/upload/v1234567890/bookstore/books/public_id.ext
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex == -1) return null;

            String fileNameWithExt = url.substring(lastSlashIndex + 1);
            int dotIndex = fileNameWithExt.lastIndexOf('.');
            if (dotIndex > 0) {
                return folder + "/" + fileNameWithExt.substring(0, dotIndex);
            }
            return folder + "/" + fileNameWithExt;
        } catch (Exception e) {
            logger.warn("Không thể trích xuất public ID từ URL: " + url, e);
            return null;
        }
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
        return pageBook;
    }
}
