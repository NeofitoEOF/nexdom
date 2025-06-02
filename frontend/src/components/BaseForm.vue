<template>
  <form @submit.prevent="onSubmit">
    <slot />
  </form>
</template>

<script setup lang="ts">
interface Props {
  onSubmit: () => void | Promise<void>;
}

defineProps<Props>();
</script>
