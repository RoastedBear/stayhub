package com.roastedbear.stayhub.global.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * springdoc 2.3.0 + Spring Boot 4.x (Spring Data 4.x) 비호환 해결 패치
 *
 * 문제: springdoc의 QuerydslPredicateOperationCustomizer가
 *       Spring Data 3.x의 TypeInformation을 참조하는데,
 *       Spring Boot 4.x (Spring Data 4.x)에서 해당 클래스가 제거됨 → NoClassDefFoundError
 *
 * 해결: 빈 정의 등록 후, 인스턴스 생성 전에 문제 빈을 레지스트리에서 제거
 *       → Spring이 해당 클래스를 로드하지 않아 오류 방지
 */
@Component
public class SpringDocQuerydslWorkaround implements BeanDefinitionRegistryPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String beanName = "queryDslQuerydslPredicateOperationCustomizer";
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 빈 팩토리 후처리 불필요
    }
}
