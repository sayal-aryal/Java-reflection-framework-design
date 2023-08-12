package application;

import framework.Service;

@Service
public class BankService implements IBankService{

    public String getBankName(){
        return "BOA";
    }
}
