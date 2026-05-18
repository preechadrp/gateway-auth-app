package com.example.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//SignController ไม่จำเป็นต้องมีแต่ทำไว้เพื่อ demo เท่านั้น เพราะระบบจะวิ่งไปที่ .yml อัตโนมัติถ้าไม่มีการสร้าง controller ไว้

@RestController
@RequestMapping("/api/sign")
public class SignController {

	@GetMapping
	public String sign() {
		return "SIGN API OK";
	}
}