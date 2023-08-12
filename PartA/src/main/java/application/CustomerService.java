package application;


import framework.Autowired;
import framework.Service;

@Service
public class CustomerService implements ICustomerService {
    @Autowired
    private IAccountService accountService;
    @Autowired
    private IBankService bankService;

    public  int getAccountNumber() {
        return accountService.getAccountNumber();
    }

    public String getBankName() {
        return bankService.getBankName();
    }


}
