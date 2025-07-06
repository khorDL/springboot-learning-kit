package com.springboot.learning.kit.validator;

import com.springboot.learning.kit.dto.request.CustomerDetailsRequest;
import com.springboot.learning.kit.exception.OrderValidationException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Component;

@Component
public class CustomerDetailsValidator implements Validator<CustomerDetailsRequest> {

    private final static String VALID_PHONE_INDEX = "^\\+?(\\d{1,3})?[-.\\s]?(\\(?\\d{3}\\)?[-.\\s]?)?(\\d[-.\\s]?){6,9}\\d$";

    @Override
    public void validate(CustomerDetailsRequest customerDetails) {
        validateName(customerDetails.getName());
        validateEmail(customerDetails.getEmail());
        validatePhoneNumber(customerDetails.getPhone());
    }

    private void validateName(String name) {
        if(name == null || name.isEmpty()){
            throw new OrderValidationException("Customer name cannot be null or empty");
        }
    }

    private void validateEmail(String email) {
        try{
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        }catch (AddressException e){
            throw new OrderValidationException("Invalid email provided: " +email);
        }
    }

    private void validatePhoneNumber(String phone) {
        if(phone == null || phone.trim().isEmpty()){
            throw new OrderValidationException("Customer phone cannot be null or empty");
        }

        if(!phone.matches(VALID_PHONE_INDEX)){
            throw new OrderValidationException("Invalid phone number provided: " + phone);
        }
    }
}
