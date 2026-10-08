package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.api.MedicineImageSearchService
import com.example.data.api.MedicineSearchResult
import com.example.data.local.MedicineEntity
import com.example.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class MedicineFormState(
    val id: Long = 0,
    val name: String = "",
    val type: String = "قرص",
    val dosage: String = "۱ عدد",
    val instructions: String = "همراه غذا با آب فراوان",
    val frequencyHours: Int = 8,
    val firstHour: Int = 8,
    val firstMinute: Int = 0,
    val isAsNeeded: Boolean = false,
    val imageUrl: String = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600&auto=format&fit=crop&q=80",
    val imageSourceTag: String = "پیش‌فرض دارویی",
    val pastelColorKey: String = "mint",
    val notes: String = "",
    val isSearchingImages: Boolean = false,
    val searchResults: List<MedicineSearchResult> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class MedicineFormViewModel(
    private val repository: MedicineRepository,
    private val alarmScheduler: AlarmScheduler,
    private val searchService: MedicineImageSearchService = MedicineImageSearchService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineFormState())
    val uiState: StateFlow<MedicineFormState> = _uiState.asStateFlow()

    init {
        // جستجوی اولیه عکس‌ها بر اساس نوع پیش‌فرض
        searchMedicineImages("قرص")
    }

    fun resetForm() {
        _uiState.value = MedicineFormState(
            id = 0L,
            name = "",
            type = "قرص",
            dosage = "۱ عدد",
            instructions = "همراه غذا با آب فراوان",
            frequencyHours = 8,
            firstHour = 8,
            firstMinute = 0,
            isAsNeeded = false,
            imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600&auto=format&fit=crop&q=80",
            imageSourceTag = "پیش‌فرض دارویی",
            pastelColorKey = "mint",
            notes = "",
            isSearchingImages = false,
            searchResults = emptyList(),
            isSaving = false,
            saveSuccess = false,
            errorMessage = null
        )
        searchMedicineImages("قرص")
    }

    fun loadMedicine(medicineId: Long) {
        if (medicineId <= 0L) {
            resetForm()
            return
        }
        viewModelScope.launch {
            val med = repository.getMedicineById(medicineId)
            if (med != null) {
                _uiState.value = _uiState.value.copy(
                    id = med.id,
                    name = med.name,
                    type = med.type,
                    dosage = med.dosage,
                    instructions = med.instructions,
                    frequencyHours = med.frequencyHours,
                    firstHour = med.firstIntakeHour,
                    firstMinute = med.firstIntakeMinute,
                    isAsNeeded = med.isAsNeeded,
                    imageUrl = med.imageUrl,
                    imageSourceTag = med.imageSourceTag,
                    pastelColorKey = med.pastelColorKey,
                    notes = med.notes,
                    errorMessage = null
                )
            } else {
                resetForm()
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateType(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun updateDosage(dosage: String) {
        _uiState.value = _uiState.value.copy(dosage = dosage)
    }

    fun updateInstructions(instructions: String) {
        _uiState.value = _uiState.value.copy(instructions = instructions)
    }

    fun updateFrequencyHours(hours: Int) {
        _uiState.value = _uiState.value.copy(frequencyHours = hours)
    }

    fun updateFirstTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(firstHour = hour, firstMinute = minute)
    }

    fun updateIsAsNeeded(isAsNeeded: Boolean) {
        _uiState.value = _uiState.value.copy(isAsNeeded = isAsNeeded)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updatePastelColorKey(key: String) {
        _uiState.value = _uiState.value.copy(pastelColorKey = key)
    }

    fun selectImage(result: MedicineSearchResult) {
        _uiState.value = _uiState.value.copy(
            imageUrl = result.imageUrl,
            imageSourceTag = "${result.source} (تأیید شده ✓)"
        )
    }

    fun setCustomImage(imageUrl: String, sourceTag: String) {
        _uiState.value = _uiState.value.copy(
            imageUrl = imageUrl,
            imageSourceTag = sourceTag
        )
    }

    fun searchMedicineImages(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingImages = true)
            try {
                val results = searchService.searchMedicineImages(query)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearchingImages = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingImages = false,
                    errorMessage = "خطا در جستجوی تصویر اینترنتی: ${e.message}"
                )
            }
        }
    }

    fun saveMedicine(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "لطفاً نام دارو را وارد کنید.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            try {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, state.firstHour)
                    set(Calendar.MINUTE, state.firstMinute)
                    set(Calendar.SECOND, 0)
                }

                val isNewMedicine = state.id <= 0L
                val entity = MedicineEntity(
                    id = if (isNewMedicine) 0L else state.id,
                    name = state.name.trim(),
                    type = state.type,
                    dosage = state.dosage.trim(),
                    instructions = state.instructions.trim(),
                    frequencyHours = state.frequencyHours,
                    firstIntakeHour = state.firstHour,
                    firstIntakeMinute = state.firstMinute,
                    firstIntakeTimestamp = cal.timeInMillis,
                    imageUrl = state.imageUrl,
                    imageSourceTag = state.imageSourceTag,
                    pastelColorKey = state.pastelColorKey,
                    isActive = true,
                    isAsNeeded = state.isAsNeeded,
                    notes = state.notes.trim()
                )

                if (isNewMedicine) {
                    repository.insertMedicine(entity)
                } else {
                    repository.updateMedicine(entity)
                }

                // زمان‌بندی مجدد و هوشمند آلارم‌ها
                alarmScheduler.rescheduleAllSmartAlarms()

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                onSuccess()
                resetForm()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "خطا در ذخیره دارو: ${e.message}"
                )
            }
        }
    }

    class Factory(
        private val repository: MedicineRepository,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedicineFormViewModel(repository, alarmScheduler) as T
        }
    }
}
