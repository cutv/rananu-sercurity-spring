package vn.rananu.spring.mvc.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.rananu.spring.mvc.annotation.JsonPayloadResult;
import vn.rananu.spring.mvc.annotation.Result;
import vn.rananu.spring.mvc.parser.JacksonParser;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class JsonPayloadResultBodyAdvice implements ResponseBodyAdvice<Object> {
    private final MessageSource messageSource;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        JsonPayloadResult annotation = returnType.getMethodAnnotation(JsonPayloadResult.class);
        return annotation != null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        JsonPayloadResult annotation = returnType.getMethodAnnotation(JsonPayloadResult.class);
        vn.rananu.spring.shared.Result.SuccessBuilder<Object> builder = vn.rananu.spring.shared.Result
                .success();
        String message = annotation.message();
        if (message != null && !message.isEmpty()) {
            //            List<Locale.LanguageRange> languageRanges = request.getHeaders().getAcceptLanguage();
//            languageRanges.isEmpty()
//                    ? Locale.getDefault()
//                    : Locale.lookup(languageRanges, LOCALES);
            Locale locale = LocaleContextHolder.getLocale();
            message = messageSource.getMessage(message, new Object[0], locale);
            builder.message(message);
        }
        vn.rananu.spring.shared.Result<Object> result = builder.build();
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            if (message != null && !message.isEmpty()) {
                String json = JacksonParser.getInstance().toJson(result);
                json = json.substring(0, json.length() - 1);
                return json + ",\"data\":" + body + "}";
            }
            return "{\"data\":" + body + "}";
        }
        return result;
    }
}
