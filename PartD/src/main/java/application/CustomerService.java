package application;


import framework.Autowired;
import framework.Qualifier;
import framework.Service;
import framework.Value;

@Service
public class CustomerService implements ICustomerService {
    @Autowired
    @Qualifier(value="accountService")
    private IAccountService accountService;
    private IBankService bankService;

    @Value("${my.property.key}")
    private String name;


    @Autowired
    public void setBankService(IBankService bankService) {
        this.bankService = bankService;
    }

    public  int getAccountNumber() {
        return accountService.getAccountNumber();
    }

    public String getBankName() {
        return bankService.getBankName();
    }

    public String getName() {
        return name;
    }
}
