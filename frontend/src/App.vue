<template>
  <div id="root">
    <NavBar />
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
</script>

<style>
#root { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; padding: 1.5rem 0; }
</style>
