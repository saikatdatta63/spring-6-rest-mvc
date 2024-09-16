package guru.springframework.spring6restmvc.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
//@ControllerAdvice
public class CustomExceptionHandler {

    //@ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException(Exception ex) {
        log.error(ex.getMessage());
        return ResponseEntity.notFound().build();
    }
}
