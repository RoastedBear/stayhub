package com.roastedbear.stayhub.global.lock;

import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Redis 기반 분산 락 서비스
 *
 * 구현 방식: SETNX (setIfAbsent) - 원자적 락 획득
 * 락 키 형식: "lock:reservation:room:{roomId}"
 * 락 타임아웃: 30초 (트랜잭션 완료 충분)
 *
 * 주의: Spring Data Redis의 SETNX 기반 구현으로,
 * 트랜잭션 커밋 전에 락이 해제될 수 있는 좁은 경합 구간이 있음.
 * DB의 existsOverlappingReservation 쿼리가 최종 안전망 역할.
 */
@Component
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private static final long LOCK_TIMEOUT_SECONDS = 30L;

    /**
     * 락 획득 후 작업 실행, 완료 시 락 해제
     *
     * @param lockKey 락 키
     * @param task    락 보호 하에 실행할 작업
     * @return 작업 결과
     * @throws BusinessException 락 획득 실패 시 ROOM_NOT_AVAILABLE
     */
    public <T> T executeWithLock(String lockKey, Callable<T> task) {
        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT_SECONDS))
        );

        if (!acquired) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        try {
            return task.call();
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("분산 락 실행 중 오류가 발생했습니다.", e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
