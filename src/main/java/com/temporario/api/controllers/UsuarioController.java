package com.temporario.api.controllers;

import com.temporario.api.dtos.UsuarioDto;
import com.temporario.api.models.UsuarioModel;
import com.temporario.api.repositories.UsuarioRepository;
import com.temporario.api.services.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/usuarios", produces = {"application/json"})
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<List<UsuarioModel>> listarUsuarios(){
        return ResponseEntity.status(HttpStatus.OK).body(usuarioRepository.findAll());
    }

    @GetMapping("/{idUsuario}")
    public  ResponseEntity<Object> buscarUsuarioId(@PathVariable(value = "idUsuario") UUID id){
        Optional<UsuarioModel> usuarioBuscado = usuarioRepository.findById(id);

        if (usuarioBuscado.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario não encontrado");
        }

        return ResponseEntity.status(HttpStatus.OK).body(usuarioBuscado.get());
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> cadastrarUsuario(@ModelAttribute @Valid UsuarioDto dadosRecebidos){
        if (usuarioRepository.findByEmail(dadosRecebidos.email()) != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email já cadastrado");
        }

        UsuarioModel usuarioModel = new UsuarioModel();
        BeanUtils.copyProperties(dadosRecebidos, usuarioModel);

        String urlImg;

        try{
            urlImg = fileUploadService.fazerUpload(dadosRecebidos.img());

        }catch (IOException e){
            throw new RuntimeException(e);
        }

        usuarioModel.setUrl_img(urlImg);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRepository.save(usuarioModel));
    }

    @PutMapping(value = "/{idUsuario}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> editarUsuario(@PathVariable(value = "idUsuario") UUID id, @ModelAttribute @Valid UsuarioDto usuarioDto){
        Optional<UsuarioModel> usuarioBuscado = usuarioRepository.findById(id);

        if (usuarioBuscado.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario não encontrado");
        }

        UsuarioModel usuarioModel = usuarioBuscado.get();
        BeanUtils.copyProperties(usuarioDto, usuarioModel);


        String urlImg;

        try{
            urlImg = fileUploadService.fazerUpload(usuarioDto.img());

        }catch (IOException e){
            throw new RuntimeException(e);
        }

        usuarioModel.setUrl_img(urlImg);

        return ResponseEntity.status(HttpStatus.OK).body(usuarioRepository.save(usuarioModel));
    }

    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Object> deletarUsuario(@PathVariable(value = "idUsuario") UUID id){
        Optional<UsuarioModel> usuarioBuscado = usuarioRepository.findById(id);

        if (usuarioBuscado.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario não encontrado");
        }

        usuarioRepository.delete(usuarioBuscado.get());
        return ResponseEntity.status(HttpStatus.OK).body("Usuario deletado com sucesso");
    }
}


