package com.example.lombok.source.processor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.example.lombok.source.Trigger")
public class SourceExampleAnnotationProcessor extends AbstractProcessor {

	private static final String ANNOTATION = "com.example.lombok.source.Trigger";

	private static final String METADATA_PATH = "lombok-source-model.properties";

	private ProcessingEnvironment env;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		this.env = env;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement annotationType = this.env.getElementUtils().getTypeElement(ANNOTATION);
		Properties properties = readMetadata();
		for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
			processElement(properties, element);
		}
		try {
			if (properties.size() > 0) {
				writeMetadata(properties);
			}
		}
		catch (IOException e) {
			this.env.getMessager().printMessage(Kind.ERROR, "Failed to write metadata  - " + e.getMessage());
		}

		return false;
	}

	private void processElement(Properties properties, Element element) {
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(((TypeElement) element).getEnclosedElements());
		// Assuming single constructor given `@Data` contract
		ExecutableElement constructor = constructors.get(0);
		for (int i = 0; i < constructor.getParameters().size(); i++) {
			properties.put(keyFor(element, i, "%s.constructor.%s.name"),
					constructor.getParameters().get(i).getSimpleName().toString());
			properties.put(keyFor(element, i, "%s.constructor.%s.type"),
					constructor.getParameters().get(i).asType().toString());
		}

	}

	private String keyFor(Element element, int i, String id) {
		return String.format(id, element.getSimpleName().toString(), i);
	}

	private void writeMetadata(Properties properties) throws IOException {
		try (OutputStream outputStream = createMetadataResource().openOutputStream()) {
			properties.store(outputStream, "lombok source model metadata");
		}
	}

	public Properties readMetadata() {
		try {
			Properties properties = new Properties();
			properties.load(getMetadataResource().openInputStream());
			return properties;
		}
		catch (IOException ex) {
			return new Properties();
		}
	}

	private FileObject getMetadataResource() throws IOException {
		return this.env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", METADATA_PATH);
	}

	private FileObject createMetadataResource() throws IOException {
		return this.env.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", METADATA_PATH);
	}
}
