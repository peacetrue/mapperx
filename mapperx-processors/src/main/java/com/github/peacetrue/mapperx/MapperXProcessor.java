package com.github.peacetrue.mapperx;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

/**
 * <pre>
 *   package com.github.peacetrue.mapperx;
 *  &nbsp;@MapperX
 *   public interface UserMapper {
 *
 *   }
 * </pre>
 * to generate
 *
 * <pre>
 *   package com.github.peacetrue.mapperx;
 *
 *   public interface UserMapperX {
 *
 *   }
 * </pre>
 */
@SupportedAnnotationTypes({"com.github.peacetrue.mapperx.MapperX"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MapperXProcessor extends AbstractProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return true;

        logger.info("处理注解{}", annotations);
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(MapperX.class);
        elementsAnnotatedWith.forEach(element -> {
            if (element.getKind() != ElementKind.INTERFACE) {
                logger.debug("元素[{}]不是接口类型", element);
                return;
            }
            String generateClassName = element.getSimpleName() + "X";
            TypeSpec generateTypeSpec = TypeSpec.interfaceBuilder(generateClassName)
                    .addSuperinterface(ClassName.get(element.asType()))
                    .addModifiers(Modifier.PUBLIC).build();
            String packageName = element.toString().split("\\." + element.getSimpleName())[0];
            try {
                logger.debug("在包[{}]下生成文件[{}.java]", packageName, generateClassName);
                JavaFile.builder(packageName, generateTypeSpec).build().writeTo(filer);
            } catch (IOException e) {
                logger.error("生成文件[{}.{}.java]异常", packageName, generateClassName, e);
            }
        });
        return true;
    }

}