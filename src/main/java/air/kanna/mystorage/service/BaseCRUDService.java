package air.kanna.mystorage.service;

import java.util.List;

import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;

public interface BaseCRUDService<T> {

    T getById(Object id);
    
    List<T> listAll(OrderBy order, Pager pager);
}
