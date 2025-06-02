import {Component, OnInit} from '@angular/core';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {
  MatAutocomplete,
  MatAutocompleteSelectedEvent,
  MatAutocompleteTrigger,
  MatOption
} from '@angular/material/autocomplete';
import {MatChip, MatChipGrid, MatChipInput, MatChipRow} from '@angular/material/chips';


@Component({
  selector: 'app-create-petition-dialog',
  imports: [
    FormsModule,
    MatDialogActions,
    MatButton,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
    MatDatepickerInput,
    MatFormField,
    MatDatepickerToggle,
    MatDatepicker,
    MatSuffix,
    MatInput,
    MatAutocomplete,
    MatOption,
    MatAutocompleteTrigger,
    MatChipInput,
    MatChipGrid,
    MatChipRow,
    MatLabel,
    MatDatepickerModule
  ],
  templateUrl: './create-petition-dialog.component.html',
  styleUrl: './create-petition-dialog.component.css'
})
export class CreatePetitionDialogComponent implements OnInit {
  title = '';
  description = '';
  petitionDate: Date = new Date();

  // Gestion des tags
  availableTags: string[] = ['Environnement', 'Société', 'Animaux', 'Santé', 'Éducation', 'Écologie', 'Ville', 'Solidarité'];
  selectedTags: string[] = [];
  filteredTags: string[] = [];
  tagInput: string = '';
  separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(public dialogRef: MatDialogRef<CreatePetitionDialogComponent>) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onCreate(): void {
    this.dialogRef.close({ title: this.title, description: this.description });
  }

  ngOnInit() {
    this.filteredTags = this.availableTags.slice();
  }

  // Ajout d'un tag depuis l'input
  addTagFromInput(event: any, forceAdd: boolean = false): void {
    const input = event.input ?? event.target;
    const value = this.tagInput?.trim() || '';

    if ((value && (forceAdd || this.availableTags.includes(value))) && !this.selectedTags.includes(value)) {
      this.selectedTags.push(value);
    }
    // réinitialise l'input
    this.tagInput = '';
    this.filterTags();
    if (input) input.value = '';
  }

  // Sélection d'un tag depuis l'autocomplete
  selectTag(event: MatAutocompleteSelectedEvent): void {
    const value = event.option.value;
    if (value && !this.selectedTags.includes(value)) {
      this.selectedTags.push(value);
    }
    this.tagInput = '';
    this.filterTags();
  }

  // Suppression d'un tag
  removeTag(tag: string): void {
    const index = this.selectedTags.indexOf(tag);
    if (index >= 0) {
      this.selectedTags.splice(index, 1);
    }
    this.filterTags();
  }

  // Filtrage dynamique des tags pour l'autocomplete
  filterTags(): void {
    const filterValue = this.tagInput?.toLowerCase() || '';
    this.filteredTags = this.availableTags
      .filter(tag =>
        tag.toLowerCase().includes(filterValue) &&
        !this.selectedTags.includes(tag)
      );
  }
}
