package com.example.lombok.source.example;

import com.example.lombok.source.Trigger;
import lombok.Data;

@Data
@Trigger
public class SampleData {

	private final String name;

	private final int description;

}
