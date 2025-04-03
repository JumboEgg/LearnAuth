package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class UserLectureNotFoundException extends DefaultException {
    public UserLectureNotFoundException(String messsage) { super(messsage); }
}
