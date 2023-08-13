package application;


import framework.*;

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

    @Scheduled(fixedRate = 5000)
    public void scheduled() {
        System.out.println("scheduled method is executing.....");
    }
}
