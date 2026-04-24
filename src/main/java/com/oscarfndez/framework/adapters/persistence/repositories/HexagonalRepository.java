package com.oscarfndez.framework.adapters.persistence.repositories;

import com.oscarfndez.framework.adapters.persistence.exceptions.ResourceNotFoundException;
import com.oscarfndez.framework.adapters.persistence.mappers.ModelEntityMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
public abstract class HexagonalRepository<M, E>  {

    @Autowired
    protected ModelEntityMapper<M, E> modelEntityMapper;

    @Autowired
    protected JpaRepository<E, UUID> jpaRepository;


    public M retrieveOne(UUID id) {
        try {
            return modelEntityMapper.entityToModel(jpaRepository.getReferenceById(id));
        } catch (JpaObjectRetrievalFailureException e) {
            throw new ResourceNotFoundException();
        }
    }

    public List<M> retrieveAny() {
        try {
            return jpaRepository.findAll()
                    .stream()
                    .map(m -> modelEntityMapper.entityToModel(m))
                    .collect(Collectors.toList());
        } catch (JpaObjectRetrievalFailureException e) {
            throw new ResourceNotFoundException();
        }
    }

    public M save(M m) {

        try {
            return modelEntityMapper.entityToModel(jpaRepository.save(modelEntityMapper.modelToEntity(m)));
        } catch (JpaObjectRetrievalFailureException e) {
            throw new ResourceNotFoundException();
        }
    }

    public void deleteOne(UUID id) {
        try {
            jpaRepository.deleteById(id);
        } catch (JpaObjectRetrievalFailureException e) {
            throw new ResourceNotFoundException();
        }
    }
}
