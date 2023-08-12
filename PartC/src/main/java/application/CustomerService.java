package application;


import framework.Autowired;
import framework.Qualifier;
import framework.Service;

@Service
public class CustomerService implements ICustomerService {
    @Autowired
    @Qualifier(value="accountService")
    private IAccountService accountService;

    private IBankService bankService;

    @Autowired
    @Qualifier(value = "bankService", type = BankService.class)
    public void setBankService(IBankService bankService) {
        this.bankService = bankService;
    }

    public  int getAccountNumber() {
        return accountService.getAccountNumber();
    }

    public String getBankName() {
        return bankService.getBankName();
    }


}
