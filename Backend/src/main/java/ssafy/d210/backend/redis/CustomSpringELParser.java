package ssafy.d210.backend.redis;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {

    /**
     * SpEL을 이용하여 동적으로 키 값을 파싱합니다.
     * @param parameterNames 메서드 파라미터 이름 배열
     * @param args 메서드 파라미터 값 배열
     * @param key SpEL 표현식으로 된 키
     * @return 파싱된 실제 키 값
     */
    public static String getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 파라미터를 SpEL context에 추가
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, String.class);
    }
}
