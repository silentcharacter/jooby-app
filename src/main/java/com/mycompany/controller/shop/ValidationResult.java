package com.mycompany.controller.shop;

public class ValidationResult
{
	public static ValidationResult OK = new ValidationResult("ok", "ok");

	public String fieldName;
	public String message;

	public ValidationResult(String fieldName, String message)
	{
		this.fieldName = fieldName;
		this.message = message;
	}
}
