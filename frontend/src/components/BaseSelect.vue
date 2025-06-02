<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">{{ label }}</label>
    <select
      :id="id"
      v-model="modelValueProxy"
      :class="['input', selectClass, { 'border-danger-500': error }]"
      @change="(e: Event) => $emit('update:modelValue', (e.target as HTMLSelectElement)?.value)"
    >
      <option v-if="placeholder" value="">{{ placeholder }}</option>
      <option v-for="option in options" :key="typeof option === 'object' && option !== null ? (option.value || String(option)) : String(option)" :value="typeof option === 'object' && option !== null ? (option.value || String(option)) : option">
        {{ display ? display(typeof option === 'object' && option !== null ? (option.value || String(option)) : option) : (typeof option === 'object' && option !== null ? (option.label || String(option)) : String(option)) }}
      </option>
    </select>
    <p v-if="error" class="mt-1 text-sm text-danger-600">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
type SelectOption = {value?: string | number, label?: string} | string | number;

interface Props {
  modelValue?: string | number;
  
  label?: string;
  
  error?: string;
  
  id?: string;
  
  options: SelectOption[];
  
  placeholder?: string;
  
  selectClass?: string;
  
  display?: (value: string | number) => string;
}

defineProps<Props>();

const modelValueProxy = defineModel<string | number>('modelValue');
</script>
